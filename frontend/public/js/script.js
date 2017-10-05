// center of Krakow
const centerOfKrakow = [50.063408, 19.943933];
const leafletMap = L.map('mapContainer').setView(centerOfKrakow, 15);

let dateFrom = null;
let dateTo = null;
let eventType = null;

const updateFilterValues = () => {
    dateFrom = document.getElementById("date-from").value;
    dateTo = document.getElementById("date-to").value;
    eventType = document.getElementById("event-type").value;
};

let lastMoveTimestamp = Date.now();

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    {
        attribution: '<a href="www.openstreetmap.org/copyright ">Terms</a> &copy; OpenStreetMap contributors',
        maxZoom: 17,
        minZoom: 4
    }).addTo(leafletMap);

const allMarkers = new L.FeatureGroup();
leafletMap.addLayer(allMarkers);

const statusToClassName = {
    "LOST": "animal-popup-status-lost",
    "FOUND": "animal-popup-status-found"
};

const updateLastMoveTimestamp = () => lastMoveTimestamp = Date.now();


leafletMap.on("zoomend", updateLastMoveTimestamp);
leafletMap.on("moveend", updateLastMoveTimestamp);
leafletMap.on("load", updateLastMoveTimestamp);

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

const onClickMarker = (event) => {
    const record = event.target.options.record;
    document.getElementsByClassName("side-bar")[0].innerHTML = `
    eventDate: ${formatDate(record.eventDate)}<br>
    publishDate: ${formatDate(record.publishDate)}<br>
    eventType: ${record.eventType}<br>
    picture:<br><img class="fullPicturePreview" src="pictures/${record.picture}"/><br>
    link: <a href="${record.link}">LINK</a><br>
    `;
};

const fetchDataFromServer = () => {

    console.log("FETCHING DATA FROM SERVER");
    const {_southWest: boundsSW, _northEast: boundsNE} = leafletMap.getBounds();
    fetch(`/rest/${boundsSW.lat}/${boundsSW.lng}/${boundsNE.lat}/${boundsNE.lng}/${dateFrom}/${dateTo}/${eventType}`)
        .then(response => {
            return response.json();
        }).then(data => {
        allMarkers.clearLayers();
        data.map(record => {
            const className = statusToClassName[record.eventType] || "";

            const icon = L.icon({
                iconUrl: `pictures_min/${record.picture}`,

                iconSize: [100, 100],
                iconAnchor: [50, 100],
                popupAnchor: [0, -100],

                className: className
            });

            allMarkers.addLayer(L.marker([record.lat, record.lon], {
                icon: icon,
                record: record
            }).on('click', onClickMarker));
        });
    }).catch(() => {
        console.log("Error when trying to fetch data");
    });
};

const twoDigitNumber = number => {
    if (number >= 0 && number < 10) {
        return "0" + number;
    }
    return number;
}

const nowDateString = () => {
    const currentDate = new Date();
    return currentDate.getUTCFullYear() + "-" +
    twoDigitNumber(currentDate.getMonth() + 1) +
    "-" + twoDigitNumber(currentDate.getUTCDate());
};

document.getElementById("date-from").value = "2017-09-01";
document.getElementById("date-to").value = nowDateString();

document.getElementById("filter-button").onclick = event => {
    updateFilterValues();
    fetchDataFromServer();
};

updateFilterValues();
setTimeout(fetchDataFromServer, 1000);
