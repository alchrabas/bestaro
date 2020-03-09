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

function monthsBetweenDates(startDate, endDate) {
    const allMonths = [];
    const currentDate = startDate.clone();
    while (currentDate.isBefore(endDate)) {
        allMonths.push(monthAndYearFromDate(currentDate));
        currentDate.add(1, 'month');
    }
    return allMonths;
}

function eventTypesArray(eventType) {
    switch (eventType) {
        case 'LOST':
        case 'FOUND':
            return [eventType, ''];
        case 'ANY':
            return ['LOST', 'FOUND'];
        default:
            throw new Error(`${eventType} is not a valid event type`);
    }
}

const getMarkers = async (minLat, minLon, maxLat, maxLon, startDate, endDate, eventType, tableName) => {
    const geohash1 = geohash.encode(minLat, minLon, 9);
    const geohash2 = geohash.encode(minLat, maxLon, 9);
    const geohash3 = geohash.encode(maxLat, minLon, 9);
    const geohash4 = geohash.encode(maxLat, maxLon, 9);
    const geohashCommonPrefix = commonPrefix([geohash1, geohash2, geohash3, geohash4]);

    const allMonths = monthsBetweenDates(startDate, endDate);

    const queryPromises = allMonths.map(yearAndMonth =>
        new aws.sdk.DynamoDB.DocumentClient().query({
            TableName: tableName,
            KeyConditionExpression: 'yearAndMonth = :yearAndMonth ' +
                (geohashCommonPrefix ? 'AND begins_with(geohash, :geohashPrefix)' : ''),
            // if common prefix is empty then ignore geohash and search the whole world
            ExpressionAttributeValues: {
                ':yearAndMonth': yearAndMonth,
                ...(geohashCommonPrefix && {':geohashPrefix': geohashCommonPrefix}),
                ':minTimestamp': startDate.valueOf(),
                ':maxTimestamp': endDate.valueOf(),
                ':minLat': minLat,
                ':minLon': minLon,
                ':maxLat': maxLat,
                ':maxLon': maxLon,
                ':eventType0': eventTypesArray(eventType)[0],
                ':eventType1': eventTypesArray(eventType)[1],
            },
            FilterExpression:
                'publishDate BETWEEN :minTimestamp AND :maxTimestamp ' +
                'AND lat BETWEEN :minLat AND :maxLat ' +
                'AND lon BETWEEN :minLon AND :maxLon ' +
                `AND eventType IN (:eventType0, :eventType1)`,
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
