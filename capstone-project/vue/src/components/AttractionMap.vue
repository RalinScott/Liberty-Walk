<template>
    <div class="map-container">
        <h2>Directions to Location</h2>



        <select v-model="selectedAttraction" v-on:change="displayDirections">
            <option v-for="attraction in attractions" :key="attraction.type_id" :value="attraction">
                {{ attraction.name }}
            </option>
        </select>

        <div v-if="selectedAttraction">
            <button onclick="CheckIn()" class="check-in-btn">Check-In</button>
        </div>


        <div ref="map" class="map"></div>
    </div>
</template>

<script>
import { Loader } from "@googlemaps/js-api-loader";
import AttractionsService from "../services/AttractionsService.js";
import CheckinService from "../services/CheckinService";


export default {

    props: [],

    data() {
        return {
            attractions: [],
            selectedAttraction: null,
            userLocation: null,
            map: null,
            directionsRenderer: null,
            currentAttractionMarker: null,
            
        };
    },

    computed: {


    },

    mounted() {

        this.createMap();
        this.addUserLocation();
        this.fetchAttractions();

    },

    methods: {

        createMap() {
            const loader = new Loader({
                apiKey: import.meta.env.VITE_REMOTE_KEY,
                libraries: ["places"],
            });

            return loader.load().then(() => {
                this.map = new google.maps.Map(this.$refs.map, {
                    center: { lat: 39.9526, lng: -75.1652 },
                    zoom: 12,
                });

                this.directionsRenderer = new google.maps.DirectionsRenderer();
                this.directionsRenderer.setMap(this.map);
            });

        },

        fetchAttractions(){
            AttractionsService.listAttractions().then((response) => {
                this.attractions = response.data;
            }).catch((error) => {
                console.error("Error fetching attractions:", error);
            });
        },

        addUserLocation() {

            navigator.geolocation.getCurrentPosition(

                (position) => {
                    this.userLocation = {
                        lat: position.coords.latitude,
                        lng: position.coords.longitude,
                    }


                    new google.maps.Marker({
                        position: this.userLocation,
                        map: this.map,
                        title: "You are here",
                        icon: {
                            url: "http://maps.google.com/mapfiles/ms/icons/blue-dot.png",
                        },
                    });

                    this.map.setCenter(this.userLocation);
                },
            );

        },

        displayDirections(){

            if (!this.selectedAttraction){
                
                if (this.currentAttractionMarker) {
                    this.currentAttractionMarker.setMap(null);
                    this.currentAttractionMarker = null;
                }

                if (this.directionsRenderer) {
                    this.directionsRenderer.setDirections({routes: [] });
                }
                return;
            }

            const destination = {
                lat: parseFloat(this.selectedAttraction.latitude),
                lng: parseFloat(this.selectedAttraction.longitude)
            };

            const request = {
                origin: this.userLocation,
                destination: destination,
                travelMode: google.maps.TravelMode.WALKING,
            };

            const directionsService = new google.maps.DirectionsService();
            directionsService.route(request, (response, status) => {
                if (status === google.maps.DirectionsStatus.OK){
                    this.directionsRenderer.setDirections(response);
                }
            });

            if (this.currentAttractionMarker){
                this.currentAttractionMarker.setMap(null);
            }
        },

        CheckIn() {
            
            if (this.selectedAttraction) {

                const checkin = {
                    user_id: this.userId,
                    attraction_id: this.selectedAttraction.id
                };

                CheckinService.createCheckin(checkin).then(() => {
                    this.selectedAttraction.checkedOff = true;

                    alert(`You checked into ${this.selectedAttraction.name}.`);
                }).catch((error) => {
                    console.error("Error during check-in:", error);
                });
            }
        }
    }

}

</script>

<style scoped>

/* previous: width: 100%;
    height: 100%;
    display: flex; changing display to grid. grid-template-columns: 1fr height: 50vh */
.map-container {
    width: 100%;
    height: 50vh;
    display: grid;
    grid-template-columns: 1fr;
    gap: 10px;
}

.map {
    width: 100%;
    height: 500px;
}

select{
    height: 5vh;
    width: 20%;
    margin: 10px;
    padding: 10px;
}

button {
    height: 5vh;
    width: 20%;
    margin: 10px;
    padding: 10px;
}
</style>

