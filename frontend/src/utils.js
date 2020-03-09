
export const twoDigitNumber = number => {
    if (number >= 0 && number < 10) {
        return "0" + number;
    }
    return number;
};

export const dateToString = (date) =>
    date.getUTCFullYear() + "-" +
    twoDigitNumber(date.getMonth() + 1) +
    "-" + twoDigitNumber(date.getUTCDate());

export const formatDate = (timestamp) => {
    if (+timestamp === 0) {
        return "-";
    }

    const date = new Date(Number.parseInt(timestamp));
    return ("0" + date.getUTCDate()).slice(-2) +
        "." + ("0" + (date.getUTCMonth() + 1)).slice(-2) +
        "." + date.getUTCFullYear();
};

export const daysRelativeToNow = (daysDifference) => {
    const date = new Date();
    date.setDate(date.getDate() + daysDifference);
    return date;
};
