package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.dtos.StockRabbitDto;
import com.example.ordersystem.common.service.StockInventoryService;
import com.example.ordersystem.common.service.StockRabbitmqService;
import com.example.ordersystem.ordering.controller.SseController;
import com.example.ordersystem.ordering.dtos.*;
import com.example.ordersystem.ordering.dtos.OrderingListResDto;
import com.example.ordersystem.ordering.entity.OrderDetail;
import com.example.ordersystem.ordering.entity.OrderStatus;
import com.example.ordersystem.ordering.entity.Ordering;
import com.example.ordersystem.ordering.repository.OrderingDetailRepository;
import com.example.ordersystem.ordering.repository.OrderingRepository;
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
    private final OrderingDetailRepository orderingDetailRepository;
    private final StockInventoryService stockInventoryService;
    private final StockRabbitmqService stockRabbitmqService;
    private final SseController sseController;

    public OrderingService(OrderingRepository orderingRepository, OrderingDetailRepository orderingDetailRepository, StockInventoryService stockInventoryService, StockRabbitmqService stockRabbitmqService, SseController sseController) {
        this.orderingRepository = orderingRepository;
        this.orderingDetailRepository = orderingDetailRepository;
        //      재고 줄여주는 로직 사용하기 위해 의존성 주입
        this.stockInventoryService = stockInventoryService;
        this.stockRabbitmqService = stockRabbitmqService;
        this.sseController = sseController;
    }


    synchronized public Ordering orderCreate(List<OrderCreateDto> dtos){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();

        for (OrderCreateDto dto : dtos) {
            //      product 서버에 api요청을 통해 product 객체를 받아와야함. --> 동기적 처리 필수
            int stockQuantity =0;
            //      배타락 걸어버림 findById 에서 로드가 걸려 동시성 이슈가 생기기때문에 배타락을 걸어놓는다
//            Product product = productRepository.findByIdForUpdate(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));
            int quantity = dto.getProductCount();
            //      동시성 이슈 고려 안한 코드
            if(stockQuantity < quantity){
                throw new IllegalArgumentException("product not enough");
            }else {
                //재고 감소 api 요청을 product서버에 보내야함. -> 비동기 처리 가능.
            }

// ;


            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
                    // 받아온 product 객체를 통해 id 값 세팅
                    .productId(dto.getProductId())
                    .quantity(dto.getProductCount())
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        orderingRepository.save(ordering);


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

        List<OrderingListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering o : orderingRepository.findByMemberEmail(email)){

            orderListResDtos.add(o.fromEntity());
        }
        return orderListResDtos;
    }

    public Ordering orderCancel(Long id){
        Ordering ordering = orderingRepository.findById(id).orElseThrow(()->new EntityNotFoundException("ordering not found"));
        ordering.cancelStatus();

        return ordering;
    }
}
