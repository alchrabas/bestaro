// center of Krakow
const centerOfKrakow = new google.maps.LatLng(50.063408, 19.943933);

let dateFrom = null;
let dateTo = null;
let eventType = null;

let visibleMarkers = [];

const googleMap = new google.maps.Map(document.getElementById('mapContainer'), {
    zoom: 12,
    center: centerOfKrakow,
    gestureHandling: 'greedy'
});

const updateFilterValues = () => {
    dateFrom = document.getElementById("date-from").value;
    dateTo = document.getElementById("date-to").value;
    eventType = document.getElementById("event-type").value;
};

let lastMoveTimestamp = Date.now();

const statusToClassName = {
    "LOST": "animal-marker-status-lost",
    "FOUND": "animal-marker-status-found"
};

const updateLastMoveTimestamp = () => lastMoveTimestamp = Date.now();

googleMap.addListener("bounds_changed", updateLastMoveTimestamp);

setInterval(() => {
    const currentTimestamp = Date.now();
    if (currentTimestamp >= lastMoveTimestamp + 3000) {
        fetchDataFromServer();
        lastMoveTimestamp = Infinity;
    }
}, 1000);

const formatDate = (timestamp) => {
    if (+timestamp === 0) {
        return "-";
    }

    const date = new Date(Number.parseInt(timestamp));
    return ("0" + date.getUTCDate()).slice(-2) +
        "-" + ("0" + (date.getUTCMonth() + 1)).slice(-2) +
        "-" + date.getUTCFullYear();
};

function onClickMarker() {
    const record = this.record;
    document.querySelector(".sidebar-content").innerHTML = `
    <div class="pure-g">
        <div class="pure-u-1-2">${Messages("details.event_date")}</div>
        <div class="pure-u-1-2">${formatDate(record.eventDate)}</div>
        <div class="pure-u-1-2">${Messages("details.post_date")}</div>
        <div class="pure-u-1-2">${formatDate(record.publishDate)}</div>
        <div class="pure-u-1-2">${Messages("details.event_type")}</div>
        <div class="pure-u-1-2">${Messages("event_type." + record.eventType)}</div>
        <div class="pure-u-1">${Messages("details.picture")}</div>
        <div class="pure-u-1"><img class="fullPicturePreview" src="pictures/${record.picture}"/></div>
        <div class="pure-u-1"><a href="${record.link}">${Messages("details.link")}</a></div>
    </div>
    `;
}

const sideBar = document.querySelector(".sidebar");

const hideSidebar = event => {
    event.preventDefault();
    sideBar.style.display = "none";
    document.querySelector("#show-sidebar").style.display = "inline";
    google.maps.event.trigger(googleMap, 'resize');
};

const showSidebar = event => {
    event.preventDefault();
    sideBar.style.display = "block";
    document.querySelector("#show-sidebar").style.display = "none";
    google.maps.event.trigger(googleMap, 'resize');
};

document.querySelector("#hide-sidebar").onclick = hideSidebar;
document.querySelector("#show-sidebar").onclick = showSidebar;
document.querySelector("#show-sidebar").style.display = "none";


const fetchDataFromServer = () => {
    console.log("FETCHING DATA FROM SERVER");
    const boundsNE = googleMap.getBounds().getNorthEast();
    const boundsSW = googleMap.getBounds().getSouthWest();
    fetch(`/rest/${boundsSW.lat()}/${boundsSW.lng()}/${boundsNE.lat()}/${boundsNE.lng()}/${dateFrom}/${dateTo}/${eventType}`)
        .then(response => {
            return response.json();
        }).then(data => {
        visibleMarkers.forEach(a => a.setMap(null));
        visibleMarkers = [];

        data.map(record => {
            const className = statusToClassName[record.eventType] || "";

            const imageSrc = `pictures_min/${record.picture}`;
            const animalMarker = new RichMarker({
                position: new google.maps.LatLng(parseFloat(record.lat), parseFloat(record.lon)),
                flat: true,
                map: googleMap,
                record: record,
                content: `<img src="${imageSrc}" class="animal-marker ${className}"/>`
            });
            visibleMarkers.push(animalMarker);
            google.maps.event.addListener(animalMarker, 'click', onClickMarker);
        });
    }).catch(e => {
        console.log("Error when trying to fetch data", e);
    });
};

const twoDigitNumber = number => {
    if (number >= 0 && number < 10) {
        return "0" + number;
    }
    return number;
};

const dateToString = (date) =>
    date.getUTCFullYear() + "-" +
    twoDigitNumber(date.getMonth() + 1) +
    "-" + twoDigitNumber(date.getUTCDate());

const weekAgoDate = new Date();
weekAgoDate.setDate(weekAgoDate.getDate() - 7);

document.getElementById("date-from").value = dateToString(weekAgoDate);
document.getElementById("date-to").value = dateToString(new Date());

document.getElementById("filter-button").onclick = event => {
    event.preventDefault();
    updateFilterValues();
    fetchDataFromServer();
};

updateFilterValues();
document.querySelector(".sidebar-content").innerHTML = `
    ${Messages("welcome_text")}
    <div class="credits">${Messages("credits")}</div>`;
setTimeout(fetchDataFromServer, 1000);
