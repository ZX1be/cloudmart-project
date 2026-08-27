import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/views/Home.vue'),
      meta: { title: 'CloudMart 商城' },
    },
    {
      path: '/login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录', guest: true },
    },
    {
      path: '/register',
      component: () => import('@/views/Register.vue'),
      meta: { title: '注册', guest: true },
    },
    {
      path: '/products/:id',
      component: () => import('@/views/ProductDetail.vue'),
      meta: { title: '商品详情' },
    },
    {
      path: '/seckill',
      component: () => import('@/views/Seckill.vue'),
      meta: { title: '秒杀活动' },
    },
    {
      path: '/coupons',
      component: () => import('@/views/Coupons.vue'),
      meta: { title: '领券中心', requiresAuth: true },
    },
    {
      path: '/cart',
      component: () => import('@/views/Cart.vue'),
      meta: { title: '购物车', requiresAuth: true },
    },
    {
      path: '/checkout',
      component: () => import('@/views/Checkout.vue'),
      meta: { title: '确认订单', requiresAuth: true },
    },
    {
      path: '/orders',
      component: () => import('@/views/Orders.vue'),
      meta: { title: '我的订单', requiresAuth: true },
    },
    {
      path: '/orders/:id',
      component: () => import('@/views/OrderDetail.vue'),
      meta: { title: '订单详情', requiresAuth: true },
    },
    {
      path: '/profile',
      component: () => import('@/views/Profile.vue'),
      meta: { title: '个人中心', requiresAuth: true },
    },
    {
      path: '/addresses',
      component: () => import('@/views/Addresses.vue'),
      meta: { title: '收货地址', requiresAuth: true },
    },
    {
      path: '/favorites',
      component: () => import('@/views/Favorites.vue'),
      meta: { title: '我的收藏', requiresAuth: true },
    },
    {
      path: '/admin/products',
      component: () => import('@/views/admin/AdminProducts.vue'),
      meta: { title: '商品管理', requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/categories',
      component: () => import('@/views/admin/AdminCategories.vue'),
      meta: { title: '分类管理', requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/orders',
      component: () => import('@/views/admin/AdminOrders.vue'),
      meta: { title: '订单管理', requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/coupons',
      component: () => import('@/views/admin/AdminCoupons.vue'),
      meta: { title: '优惠券管理', requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/seckill',
      component: () => import('@/views/admin/AdminSeckill.vue'),
      meta: { title: '秒杀管理', requiresAuth: true, requiresAdmin: true },
    },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (to.meta.title) {
    document.title = `${to.meta.title} - CloudMart 云商城`
  }
  if (to.meta.requiresAuth && !token) {
    return '/login'
  }
  if (to.meta.guest && token) {
    return '/'
  }
  if (to.meta.requiresAdmin && role !== 'ADMIN') {
    return '/'
  }
  return true
})

export default router
