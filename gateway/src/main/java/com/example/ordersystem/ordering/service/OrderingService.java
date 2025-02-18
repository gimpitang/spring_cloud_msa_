package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.dtos.StockRabbitDto;
import com.example.ordersystem.common.service.StockInventoryService;
import com.example.ordersystem.common.service.StockRabbitmqService;
import com.example.ordersystem.ordering.controller.SseController;
import com.example.ordersystem.ordering.dtos.*;
import com.example.ordersystem.member.entity.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.ordering.dtos.OrderingListResDto;
import com.example.ordersystem.ordering.entity.OrderDetail;
import com.example.ordersystem.ordering.entity.OrderStatus;
import com.example.ordersystem.ordering.entity.Ordering;
import com.example.ordersystem.ordering.repository.OrderingDetailRepository;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import com.example.ordersystem.product.entity.Product;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final OrderingDetailRepository orderingDetailRepository;
    private final ProductRepository productRepository;
    private final StockInventoryService stockInventoryService;
    private final StockRabbitmqService stockRabbitmqService;
    private final SseController sseController;

    public OrderingService(OrderingRepository orderingRepository, MemberRepository memberRepository, OrderingDetailRepository orderingDetailRepository, ProductRepository productRepository, StockInventoryService stockInventoryService, StockRabbitmqService stockRabbitmqService, SseController sseController) {
        this.orderingRepository = orderingRepository;
        this.memberRepository = memberRepository;
        this.orderingDetailRepository = orderingDetailRepository;
        this.productRepository = productRepository;
        //      재고 줄여주는 로직 사용하기 위해 의존성 주입
        this.stockInventoryService = stockInventoryService;
        this.stockRabbitmqService = stockRabbitmqService;
        this.sseController = sseController;
    }


    public Ordering orderCreate(List<OrderCreateDto> dtos){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("Member not found"));
//        //      1. cascading 없이 db 저장 (orderingDetailRepository 필요함
//        //      Ordering 객체 생성 및 save
//        Ordering ordering = Ordering.builder()
//                .member(member)
//                .build();
//        orderingRepository.save(ordering);
//
//        //      OrderingDetail 객체 생성 및 save
//        for (OrderCreateDto dto : dtos) {
//            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));
//            if(product.getStockQuantity() < dto.getProductCount()){
//                throw new IllegalArgumentException("product not enough");
//            }else {
//                //재고 감소 로직
//                product.updateStockQuantity(dto.getProductCount());
//            }
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .ordering(ordering)
//                    .product(product)
//                    .quantity(dto.getProductCount())
//                    .build();
//            orderingDetailRepository.save(orderDetail);
//        }



        //      2. cascading 사용하여 db 저장
        //      Ordering 객체 생성하면서 OrderingDetail
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        for (OrderCreateDto dto : dtos) {
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));
            //      배타락 걸어버림 findById 에서 로드가 걸려 동시성 이슈가 생기기때문에 배타락을 걸어놓는다
//            Product product = productRepository.findByIdForUpdate(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));
            int quantity = dto.getProductCount();
            //      동시성 이슈 고려 안한 코드
            if(product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("product not enough");
            }else {
                //재고 감소 로직
                product.updateStockQuantity(dto.getProductCount());
            }

//            //      동시성 이슈를 고려한 코드
//            //      redis를 통한 재고 관리 및 재고 잔량 확인
//            int newQuantity = stockInventoryService.decreaseStock(product.getId(), quantity);
//            if(newQuantity<0){
//                throw new IllegalArgumentException("재고 부족");
//            }
//
//            //      rdb 동기화(rabbitmq)
//            StockRabbitDto stockRabbitDto = StockRabbitDto.builder()
//                    .productId(product.getId()).productCount(quantity)
//                    .build();
//            //      아래가 실행되는 순간 queue에 들어간다.
//            stockRabbitmqService.publish(stockRabbitDto);


            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        Ordering ordering1 = orderingRepository.save(ordering);

        //      sse를 통한 admin계정에 메시지 발송
        sseController.publishMessage(ordering1.fromEntity(),"admin@naver.com");

        //----공통
        return ordering;
    }

    //      동시성 처리 전
//    public Ordering orderCreate(List<OrderCreateDto> dtos){
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("Member not found"));
////        //      1. cascading 없이 db 저장 (orderingDetailRepository 필요함
////        //      Ordering 객체 생성 및 save
////        Ordering ordering = Ordering.builder()
////                .member(member)
////                .build();
////        orderingRepository.save(ordering);
////
////        //      OrderingDetail 객체 생성 및 save
////        for (OrderCreateDto dto : dtos) {
////            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));
////            if(product.getStockQuantity() < dto.getProductCount()){
////                throw new IllegalArgumentException("product not enough");
////            }else {
////                //재고 감소 로직
////                product.updateStockQuantity(dto.getProductCount());
////            }
////            OrderDetail orderDetail = OrderDetail.builder()
////                    .ordering(ordering)
////                    .product(product)
////                    .quantity(dto.getProductCount())
////                    .build();
////            orderingDetailRepository.save(orderDetail);
////        }
//
//
//
//        //      2. cascading 사용하여 db 저장
//        //      Ordering 객체 생성하면서 OrderingDetail
//        Ordering ordering = Ordering.builder()
//                .member(member)
//                .build();
//
//        for (OrderCreateDto dto : dtos) {
//            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));
//            if(product.getStockQuantity() < dto.getProductCount()){
//                throw new IllegalArgumentException("product not enough");
//            }else {
//                //재고 감소 로직
//                product.updateStockQuantity(dto.getProductCount());
//            }
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .ordering(ordering)
//                    .product(product)
//                    .quantity(dto.getProductCount())
//                    .build();
//            ordering.getOrderDetails().add(orderDetail);
//        }
//        orderingRepository.save(ordering);
//
//        //----공통
//        return ordering;
//    }

    public List<OrderingListResDto> orderList(){

        // 나 혼자 했덩거----------------------------------------------------------
//        List<Ordering> orderings = orderingRepository.findAll();
//        List<OrderingListResDto> orderingList = new ArrayList<>();
//        for(int i =0; i< orderings.size(); i++){
//
//            OrderingListResDto orderingListDto = new OrderingListResDto();
//            orderingListDto = OrderingListResDto.builder()
//                    .orderId(orderings.get(i).getId())
//                    .memberEmail(orderings.get(i).getMember().getEmail())
//                    .orderStatus(orderings.get(i).getOrderStatus().toString())
//                    .orderDetails(orderings.get(i).)
//                    .build();
//        }
        // 나 혼자 했덩거----------------------------------------------------------
        List<Ordering> orderings = orderingRepository.findAll();
        List<OrderingListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering o : orderings){

            orderListResDtos.add(o.fromEntity());
        }
        return orderListResDtos;
    }

    public List<OrderingListResDto> myOrders(){

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("Member not found"));
        List<Ordering> orderings = member.getOrderingList();
        List<OrderingListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering o : orderings){

            orderListResDtos.add(o.fromEntity());
        }
            //      o.fromEntity 안에 들어갈 내용
//        List<OrderDetailResDto> orderDetailResDtos = new ArrayList<>();
//        for(OrderDetail od : o.getOrderDetails()){
//            OrderDetailResDto orderDetailResDto = OrderDetailResDto.builder()
//                    .detailId(od.getId())
//                    .productName(od.getProduct().getName())
//                    .count(od.getQuantity())
//                    .build();
//            orderDetailResDtos.add(orderDetailResDto);
//        }
//        OrderingListResDto orderDto = OrderingListResDto
//                .builder()
//                .orderId(o.getId())
//                .memberEmail(o.getMember().getEmail())
//                .orderStatus(o.getOrderStatus().toString())
//                .orderDetails(orderDetailResDtos)
//                .build();
        return orderListResDtos;

    }

    public Ordering orderCancel(Long id){
        Ordering ordering = orderingRepository.findById(id).orElseThrow(()->new EntityNotFoundException("ordering not found"));
        ordering.cancelStatus();
        for(OrderDetail orderDetail : ordering.getOrderDetails()){
//            int count = orderDetail.getProduct().getStockQuantity() + orderDetail.getQuantity();
            orderDetail.getProduct().cancelOrder(orderDetail.getQuantity());
        }
        return ordering;
    }
}
