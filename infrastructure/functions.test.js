const aws = require('@pulumi/aws');
const awsMock = require('aws-sdk-mock');
const functions = require('./functions');
const fs = require('fs');
const moment = require('moment');

const readJson = (fileName) => JSON.parse(fs.readFileSync(fileName));

describe('Geographic records', () => {
    awsMock.setSDKInstance(aws.sdk);

    it('should add record', async () => {
        let putObjectMock = jest.fn();
        awsMock.mock('DynamoDB.DocumentClient', 'put',
            (params, cb) => cb(null, putObjectMock(params)));
        const inputRecord = readJson('test_files/input_record.json');
        const expected = readJson('test_files/expected_put_request.json');

        await functions.addRecord(inputRecord, 'records');

        expect(putObjectMock).toHaveBeenCalledWith(expected);
    });

    it('should query for records from a single month and year', async () => {
        let queryMock = jest.fn();
        awsMock.mock('DynamoDB.DocumentClient', 'query',
            (params, cb) => {
                queryMock(params);
                cb(null, {Items: []});
            });

        await functions.getMarkers(50.129754, 19.557483, 50.134368, 19.570763,
            moment('2020-02-01'), moment('2020-02-18'), 'LOST', 'records');

        expect(queryMock.mock.calls[0][0].ExpressionAttributeValues[':yearAndMonth']).toBe(202002);
        expect(queryMock.mock.calls[0][0].ExpressionAttributeValues[':geohashPrefix']).toBe('u2vvj');
    });

    it('should query for records from multiple months', async () => {
        let queryMock = jest.fn();
        awsMock.mock('DynamoDB.DocumentClient', 'query',
            (params, cb) => {
                queryMock(params);
                cb(null, {Items: []});
            });

        await functions.getMarkers(50.129754, 19.557483, 50.134368, 19.570763,
            moment('2019-12-01'), moment('2020-02-18'), 'LOST', 'records'
        );
        expect(queryMock.mock.calls[0][0].ExpressionAttributeValues[':yearAndMonth']).toBe(201912);
        expect(queryMock.mock.calls[1][0].ExpressionAttributeValues[':yearAndMonth']).toBe(202001);
        expect(queryMock.mock.calls[2][0].ExpressionAttributeValues[':yearAndMonth']).toBe(202002);
    });

    afterEach(() => {
        awsMock.restore('DynamoDB.DocumentClient');
    });
});
