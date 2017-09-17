// center of Krakow
const centerOfKrakow = [50.063408, 19.943933];
const leafletMap = L.map('mapContainer').setView(centerOfKrakow, 15);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    {
        attribution: '<a href="www.openstreetmap.org/copyright ">Terms</a> &copy; OpenStreetMap contributors',
        maxZoom: 17,
        minZoom: 4
    }).addTo(leafletMap);

const statusToClassName = {
    "LOST": "animal-popup-status-lost",
    "FOUND": "animal-popup-status-found",
    "SEEN": "animal-popup-status-found"
};


fetch("/rest/").then(response => {
    return response.json();
}).then(data => {
    data.map(record => {
        const className = statusToClassName[record.status] || "";

        L.popup({
            closeButton: false,
            className: className
        })
            .setLatLng([record.lat, record.lon])
            .setContent(`<img class="animalPicture" src="pictures/${record.picture}"/>`)
            .addTo(leafletMap);
    });
}).catch(() => {
    console.log("Booo");
});
