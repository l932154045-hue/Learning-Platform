<script setup lang="ts">
</script>

<template>
  <footer class="app-footer">
    <div class="container footer-inner">
      <div class="footer-brand">
        <span class="footer-logo">📚 学途</span>
        <p class="footer-desc">课程源自一线工程师，内容每月更新</p>
      </div>
      <div class="footer-links">
        <div class="link-group">
          <h4>课程分类</h4>
          <a>前端开发</a>
          <a>后端开发</a>
          <a>数据科学</a>
          <a>人工智能</a>
        </div>
        <div class="link-group">
          <h4>关于我们</h4>
          <a>关于学途</a>
          <a>帮助中心</a>
          <a>用户协议</a>
          <a>隐私政策</a>
        </div>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; 2026 学途在线课程平台. All rights reserved.</p>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: var(--color-primary);
  color: rgba(255, 255, 255, 0.7);
  margin-top: var(--space-3xl);
}
.footer-inner {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3xl);
  padding: var(--space-3xl) var(--space-lg);
}
.footer-brand {
  max-width: 280px;
}
.footer-logo {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: white;
}
.footer-desc {
  margin-top: var(--space-sm);
  font-size: var(--font-size-sm);
}
.footer-links {
  display: flex;
  gap: var(--space-3xl);
}
.link-group h4 {
  color: white;
  font-size: var(--font-size-sm);
  font-weight: 600;
  margin-bottom: var(--space-sm);
}
.link-group a {
  display: block;
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-xs);
  cursor: pointer;
  transition: color 0.15s;
}
.link-group a:hover {
  color: white;
}
.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  text-align: center;
  padding: var(--space-lg);
  font-size: var(--font-size-xs);
}
</style>
