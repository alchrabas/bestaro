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


const fetchDataFromServer = () => {

    console.log("FETCHING DATA FROM SERVER");
    const {_southWest: boundsSW, _northEast: boundsNE} = leafletMap.getBounds();
    fetch(`/rest/${boundsSW.lat}/${boundsSW.lng}/${boundsNE.lat}/${boundsNE.lng}`)
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

            allMarkers.addLayer(L.marker([record.lat, record.lon], {icon: icon}).bindPopup(`
                eventDate: ${record.eventDate}<br>
                publishDate: ${record.publishDate}<br>
                eventType: ${record.eventType}<br>
                picture: <img class="fullPicturePreview" src="pictures/${record.picture}"/><br>
            `));
        });
    }).catch(() => {
        console.log("Error when trying to fetch data");
    });
};

setTimeout(fetchDataFromServer, 1000);
