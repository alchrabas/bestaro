const geohash = require('ngeohash');
const aws = require('@pulumi/aws');
const commonPrefix = require('common-prefix');
const moment = require('moment');

const ID_DELIMITER = '-';

const recordIdToString = (recordId) => {
    return recordId.service + ID_DELIMITER + recordId.id;
};

const recordIdFromString = (str) => {
    return {
        service: str.split(ID_DELIMITER)[0],
        id: str.split(ID_DELIMITER)[1],
    };
};

function monthAndYearFromDate(eventDate) {
    return +eventDate.format('YYYYMM');
}

const addRecord = async (r, tableName) => {
    const eventDate = moment(r.eventDate);
    const coords = r.fullLocation.coordinate;

    return new aws.sdk.DynamoDB.DocumentClient().put({
        TableName: tableName,
        Item: {
            id: recordIdToString(r.recordId),
            eventType: r.eventType,
            animalType: r.animalType,
            pictures: r.pictures,
            eventDate: r.eventDate,
            publishDate: r.postDate,
            link: r.link,
            yearAndMonth: monthAndYearFromDate(eventDate),
            lat: coords.lat,
            lon: coords.lon,
            geohash: geohash.encode(coords.lat, coords.lon, 9),
        },
    }).promise();
};

const getMarkers = async (minLat, minLon, maxLat, maxLon, timestampFrom, timestampTo, eventType, tableName) => {
    const geohash1 = geohash.encode(minLat, minLon, 9);
    const geohash2 = geohash.encode(minLat, maxLon, 9);
    const geohash3 = geohash.encode(maxLat, minLon, 9);
    const geohash4 = geohash.encode(maxLat, maxLon, 9);
    const geohashCommonPrefix = commonPrefix([geohash1, geohash2, geohash3, geohash4]);

    const currentDate = moment(timestampFrom);
    const endDate = moment(timestampTo);
    const allMonths = [];
    while (currentDate.isBefore(endDate)) {
        allMonths.push(monthAndYearFromDate(currentDate));
        currentDate.add(1, 'month');
    }
    console.log(allMonths);
    const queryPromises = allMonths.map(yearAndMonth =>
        new aws.sdk.DynamoDB.DocumentClient().query({
            TableName: tableName,
            KeyConditionExpression: 'yearAndMonth = :yearAndMonth AND begins_with(geohash, :geohashPrefix)',
            ExpressionAttributeValues: {
                ':yearAndMonth': yearAndMonth,
                ':geohashPrefix': geohashCommonPrefix,
                ':minTimestamp': timestampFrom,
                ':maxTimestamp': timestampTo,
                ':minLat': minLat,
                ':minLon': minLon,
                ':maxLat': maxLat,
                ':maxLon': maxLon,
            },
            FilterExpression:
                'eventDate BETWEEN :minTimestamp AND :maxTimestamp ' +
                'AND lat BETWEEN :minLat AND :maxLat ' +
                'AND lon BETWEEN :minLon AND :maxLon',
        }).promise()
    );
    const results = await Promise.all(queryPromises);
    return results.map(result => result.Items)
        .flat()
        .map(r => ({
            id: r.id,
            eventDate: r.eventDate,
            publishDate: r.publishDate,
            picture: r.pictures[0],
            minPicture: r.pictures[0],
            eventType: r.eventType,
            lat: r.lat,
            lon: r.lon,
            link: r.link,
            distance: 1.0,
        }));
};

module.exports = {
    addRecord,
    getMarkers,
    recordIdFromString,
    recordIdToString,
};
