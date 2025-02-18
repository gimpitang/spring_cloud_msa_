<!-- App.vue가 루트컴포넌트 -->
<!-- 진입점 역할을 한다. -->
<template>
  <!-- routerview를 통해 router파일에서 설정한 url경로로 화면을 여기서 보여주겠다는 뜻. -->
  <!-- vuetify에서는 모든 태그의 최상단에 v-app으로 감싸져있어야 정상적으로 동작 -->
  <v-app>
    <!-- 헤더 -->
    <HeaderComponent/>
      <v-main>
      <!-- <router-view></router-view> -->
      <router-view/>
      </v-main>
    <!-- 푸터 -->
    <FooterComponent/>
  </v-app>
</template>

<script>
// Import는 script에서 진행
import HeaderComponent from './components/HeaderComponent.vue';
import FooterComponent from './components/FooterComponent.vue';
export default{
  // 화면을 import한 이후에 template에서 사용하려고 할 때에는 components에 나열열
  components: {
    HeaderComponent, FooterComponent
  }
}
</script>

<style>

</style>
