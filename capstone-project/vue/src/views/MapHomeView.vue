<template>
    <div class="maphome">
      <h1>Attraction Map</h1>
      <!---<router-link v-bind:to="{ name: 'mapdata' }">Map Data</router-link><br>-->
      <!-- <router-link v-bind:to="{ name: 'mapdirections' }">Map Directions</router-link> -->

      <map-display/>

    </div>

    
      
  </template>
  
  <script>
  import MapDisplay from "../components/MapDisplay.vue"

  export default {

    components:{
      MapDisplay
    }
    
  };
  </script> 