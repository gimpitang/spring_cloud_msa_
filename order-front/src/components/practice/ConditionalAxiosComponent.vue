<template>
    <h1>vue 조건식</h1>
    <button v-if="!isLogined" @click="doLogin()">로그인</button>
    <button v-if="isLogined" @click="doLogout()">로그아웃</button><br>
    
    <div v-if="!isLogined">로그인해주세요</div>
    <div v-if="isLogined">환영합니다. 고갱님</div>

    <h1>상품목록 조회</h1>
    <v-simple-table>
        <thead>
            <tr>
                <th>id</th>
                <th>name</th>
                <th>price</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="product in productList" :key="product.id">
                <td>{{ product.id }}</td>&nbsp;&nbsp;&nbsp;
                <td>{{ product.name }}</td>&nbsp;&nbsp;&nbsp;
                <td>{{ formatPrice(product.price) }}</td>
            </tr>
        </tbody>
    </v-simple-table>
</template>
<script>
import axios from 'axios';
export default{
    data(){
        return{
            isLogined:false,
            productList: [{id:1, name:"apple", price:200000},{id:2, name:"banana", price:100000},{id:3, name:"orange", price:300000}]
            
        }
    },
    async created(){
        //  axios.get요청
        //  productLsit = respose.data
        const respose = await axios.get("http://localhost:8080/product/list")
        console.log(respose)
        this.productList= respose.data.content
    },
    methods: {
        doLogin(){
            this.isLogined = true;
        },
        doLogout(){
            this.isLogined = false;
            
        },
        formatPrice(value){
            return new Intl.NumberFormat("ko-KR").format(value);
        }
    }
}</script>