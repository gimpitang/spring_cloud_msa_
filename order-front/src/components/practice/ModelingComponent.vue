<template> 
<h1> hello world  {{ language }}</h1>
<!-- v-model 문법을 사용하면 양방향 데잍터 바인딩 -->
<input type="text" v-model = "value">
<button @click="showValue()">변수 변경사항 확인</button><br>
<button @click="changeValue()">변수 변경</button>
</template>

<script>
export default{
    data(){
        return{
            language: "java",
            value: "python"
        }
    },
        //  화면이 생성되기 전에에 실행되는 hook 함수수
    created(){
        alert("created 함수 호출");
    },
    methods:{
        showValue(){
            alert(this.value);
        },
        changeValue(){
            this.value="C++";
        }
    }
} 
</script>