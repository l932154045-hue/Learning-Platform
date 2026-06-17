package com.learning.cart.service.impl;

import com.learning.cart.cache.CartCacheService;
import com.learning.cart.client.CourseClient;
import com.learning.cart.entity.Cart;
import com.learning.cart.mapper.CartMapper;
import com.learning.common.core.exception.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl 单元测试")
class CartServiceImplTest {

    @Mock
    private CartMapper cartMapper;
    @Mock
    private CartCacheService cartCacheService;
    @Mock
    private CourseClient courseClient;
    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    @DisplayName("添加购物车 — 正常插入并写缓存")
    void shouldInsertAndAddToCache() {
        when(cartMapper.insert(any(Cart.class))).thenReturn(1);

        cartService.add(1L, 100L);

        verify(cartMapper).insert(any(Cart.class));
        verify(cartCacheService).addToCart(1L, 100L);
    }

    @Test
    @DisplayName("添加购物车 — 重复插入抛 BizException")
    void shouldThrowExceptionWhenDuplicate() {
        when(cartMapper.insert(any(Cart.class)))
                .thenThrow(new DuplicateKeyException("Duplicate entry"));

        assertThrows(BizException.class, () -> cartService.add(1L, 100L));
        verify(cartCacheService, never()).addToCart(anyLong(), anyLong());
    }

    @Test
    @DisplayName("添加购物车 — 缓存写入失败不阻断主流程")
    void shouldNotThrowWhenCacheFails() {
        when(cartMapper.insert(any(Cart.class))).thenReturn(1);
        doThrow(new RuntimeException("redis down"))
                .when(cartCacheService).addToCart(1L, 100L);

        assertDoesNotThrow(() -> cartService.add(1L, 100L));
        verify(cartMapper).insert(any(Cart.class));
    }

    @Test
    @DisplayName("移出购物车 — 同时删除DB和缓存")
    void shouldRemoveFromCart() {
        when(cartMapper.delete(any())).thenReturn(1);

        cartService.remove(1L, 100L);

        verify(cartMapper).delete(any());
        verify(cartCacheService).removeFromCart(1L, 100L);
    }

    @Test
    @DisplayName("清空购物车 — 同时清理DB和缓存")
    void shouldClearCart() {
        when(cartMapper.delete(any())).thenReturn(3);

        cartService.clear(1L);

        verify(cartMapper).delete(any());
        verify(cartCacheService).clearCart(1L);
    }
}
