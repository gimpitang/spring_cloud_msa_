<template>
<h1>hello world</h1>
<button @click="moveComponent()">화면이동</button>
<h1>{{ count1 }}</h1>
<button @click="incrementCount1()">incrementCount1</button>
<h1>{{ doubleCount1 }}</h1>
</template>
<script>
export default{
    data(){
        return{
            value : "hello",
            count1 : "0"
        }
    },

    // //      화면이 열리기 전 (vue인스턴스가 생성되기 전) 실행되는 함수
    // created(){
    //     alert("훅 함수 실습입니다."+this.value);
    // },
    // //      컴포넌트가 화면에서 제거될 때 실행되는 함수.(화면종료, 라우트 이동 시)
    // beforeUnmount(){
    //     alert("화면이 종료될 때 쓰이는 함수수")
    // },
    // //      특정 변수값의 변경사항을 인지하는 hook 함수
    // watch:{
    //     //      인지하고자 하는 변수값과 일치하는 함수명 사용용
    //     count1(){
    //         alert("count1값 변경")
    //     }
    // },
    //      종속된 데이터가 변경될 때 함수를 다시 실행하여 값을 계산하는 함수
    computed:{
        doubleCount1(){
            return this.count1 * 2;

        }
    },
    //      화면의 변경사항을 인지하는 hook함수
    // updated(){
    //     alert("화면의 요소가 변경되었습니다.")

    // },

    methods:{
        moveComponent(){
            this.$router.push("/practice/vuetify");
        },
        incrementCount1(){
            this.count1 ++;
        }
    },
    
}
</script>
