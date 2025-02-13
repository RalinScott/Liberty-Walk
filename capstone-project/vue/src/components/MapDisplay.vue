<template>
  <body>
   
 
  <div class="dropdown">
    <select v-on:change="onCategoryChange($event)">
      <option value="all">All Venues</option>
      <option v-for="type in types" :key="type.type_id" :value="type.type_id">
        {{ type.name }}


      </option>
    </select>
  </div>


  <div class="map-container">
    <div id="map" ref="map"></div>
  </div>


 
    <div class="card-container">
      <div class="card" v-for="(marker, index) in filteredMarkers" :key="index" :id="'card-' + index">
        <div class="card-content">
        <img :src="marker.image" alt="Image of attraction"/>
        <h3>{{ marker.name }}</h3>
        <p>{{ marker.description }}</p>
        <p>Hours: {{ marker.hoursOfOperation }}</p>
        <p>Address: {{ marker.address }}</p>
        <p>Social Media: {{ marker.socialMedia }}</p>
        <button class="details" v-on:click="navigateToDirections(marker)">Get Directions</button>  
    </div>
    </div>
    </div>
 
  </body>
</template>

<script>
import axios from 'axios';
import { Loader } from "@googlemaps/js-api-loader";

let gmarkers1 = [];

export default {
  data() {
    return {
      map: null,
      markers: [],
      markersData: [],
      types: [
        { type_id: 1, name: 'Bars' },
        { type_id: 2, name: 'Museums' },
        { type_id: 3, name: 'Stadiums' },
        { type_id: 4, name: 'Parks' },
        { type_id: 5, name: 'Restaurants' }
      ],
      selectedGroup: "all"
    };
  },

  computed: {

    filteredMarkers() {
      if (this.selectedGroup === "all") {
        return this.markersData;
      }
      return this.markersData.filter(marker => marker.typeId === parseInt(this.selectedGroup));
    },
  },

  mounted() {

    const loader = new Loader({
      apiKey: import.meta.env.VITE_REMOTE_KEY, // API key
      libraries: ["places"],
    });

    loader.load().then(() => {
      this.map = new google.maps.Map(this.$refs.map, {
        center: { lat: 39.9526, lng: -75.1652 }, //Philly
        zoom: 12,
      });

      this.fetchAttractions();
      this.addUserLocation();

    });
  },

  methods: {

    fetchAttractions() {

      axios.get('/attractions/').then(response => {
        this.markersData = response.data.map(attraction => ({
          id: attraction.id,
          name: attraction.name,
          description: attraction.description,
          hoursOfOperation: attraction.hoursOfOperation,
          address: attraction.address,
          socialMedia: attraction.socialMedia,
          image: new URL(`/src/assets/images/${attraction.image}`, import.meta.url).href,
          typeId: attraction.typeId,
          latitude: attraction.latitude,
          longitude: attraction.longitude,
        }));

        this.displayMarkers();

      }).catch(error => {
        console.error("Error fetching attractions:", error);
      });
    },

    createMarker(markerData) {

      const marker = new google.maps.Marker({
        position: { lat: markerData.latitude, lng: markerData.longitude },
        map: this.map,
        title: markerData.name
      });

      marker.category = markerData.typeId;
      gmarkers1.push(marker);
      return marker;
    },

    displayMarkers() {

      gmarkers1.forEach(marker => marker.setMap(null));
      gmarkers1 = [];

      this.filteredMarkers.forEach(markerData => {
        this.createMarker(markerData);
      });

      this.filterMarkers(this.selectedGroup);

    },

    filterMarkers(category) {

      for (let i = 0; i < gmarkers1.length; i++) {
        const marker = gmarkers1[i];
        if (category === "all" || marker.category === parseInt(category)) {
          marker.setVisible(true);
        } else {
          marker.setVisible(false);
        }
      }
    },

    onCategoryChange(event) {
      const category = event.target.value;
      this.selectedGroup = category;
      this.filterMarkers(category);
    },

    addUserLocation() {

      navigator.geolocation.getCurrentPosition((position) => {
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

    navigateToDirections(marker) {
      this.$router.push({
        name: 'attnav',
        params: { aId: marker.id}
      });
    }
  },
};
</script>

<style scoped>






/*.map-container {
  display: grid;
  grid-template-columns: 5fr;
  grid-template-areas:
    "notmap map map map notmap"
  ;
  width: 100%;
  height: 50vh;
  gap: 10px;
  overflow: hidden;
}*/




 .card-container{
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  grid-template-areas:
    "map map map map map"
    "card card card card card";
  width: 100%;
  padding: 16px;
  gap: 16px;
  box-sizing: border-box;
  grid-auto-rows: 1fr;
  align-items: stretch;
  overflow: hidden;
} 



.card {
  display: inline-block;
  widows: 150px;
}



.card-content{
  background-color: var(--neutral-color);
  border: 1px solid #F7CD75;
  border-radius: 8px;
  height: 100%;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  align-items: center;
  transition: box-shadow 0.3s ease;
}


.card-container img {
  width: 100%;
  height: 210px;
  object-fit: cover;
  border-radius: 4px;


}


.card-container h3 {
  color: var(--text-color);
  margin: 12px 0;
}
.card-container p {
  color: var(--text-color);
  text-align: center;
}


details[open] summary {
  border-bottom: 2px solid #6e3f13;
}


.card-content:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}


.dropdown {
  grid-area: sidebar;
  padding: 10px;
}


.map {
  grid-area: map;
  height: 50vh;
  width: 100%;
  position: relative;
}


.dropdown select {
  font-size: 1.2rem;
  padding: 10px;
  border: 1px solid #F7CD75;
  border-radius: 5px;
  background-color: #FFFFFF;
  color: #6E3F13;
  width: 35%;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  font-family: 'Georgia', serif;
}


.dropdown select:hover {
  background-color: #F7CD75;
  border-color: #EB473D;
  color: #6E3F13;
}




</style>
