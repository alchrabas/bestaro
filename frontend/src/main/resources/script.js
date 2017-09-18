// center of Krakow
const centerOfKrakow = [50.063408, 19.943933];
const leafletMap = L.map('mapContainer').setView(centerOfKrakow, 15);

let lastMoveTimestamp = Date.now();

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    {
        attribution: '<a href="www.openstreetmap.org/copyright ">Terms</a> &copy; OpenStreetMap contributors',
        maxZoom: 17,
        minZoom: 4
    }).addTo(leafletMap);

const statusToClassName = {
    "LOST": "animal-popup-status-lost",
    "FOUND": "animal-popup-status-found",
    "SEEN": "animal-popup-status-seen"
};

const updateLastMoveTimestamp = () => {
    lastMoveTimestamp = Date.now();
};

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


const fetchDataFromServer = () => {

    console.log("FETCHING DATA FROM SERVER");
    const {_southWest: boundsSW, _northEast: boundsNE} = leafletMap.getBounds();
    fetch(`/rest?minlat=${boundsSW.lat}&minlon=${boundsSW.lng}&maxlat=${boundsNE.lat}&maxlon=${boundsNE.lng}`)
        .then(response => {
            return response.json();
        }).then(data => {
        data.map(record => {
            const className = statusToClassName[record.status] || "";

            const icon = L.icon({
                iconUrl: `pictures/${record.picture}`,

                iconSize: [100, 100],
                iconAnchor: [50, 100],
                popupAnchor: [0, -100],

                className: className
            });

            L.marker([record.lat, record.lon], {icon: icon}).addTo(leafletMap);
        });
    }).catch(() => {
        console.log("Error when trying to fetch data");
    });
};

setTimeout(fetchDataFromServer, 1000);
