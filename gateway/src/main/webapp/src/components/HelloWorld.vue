<template>
  <div class="hello">
    <h1>{{ msg }}</h1>
    <button @click="initWebSocket">open</button>
    <form action="ua/login" method="post">
        <input type="text" name="username" placeholder="username" />
        <input type="password" name="password" placeholder="password" />
        <input type="checkbox" name="remember-me">Remember
        <input type="text" name="verifyCode" placeholder="verify code" />
        <img src="/ua/verify-code-img.jpg" alt="verify code" height="25px" width="75px">
        <div>
            <a href="#">forget Password</a>
            <input type="submit" value="Login">
        </div>
    </form>
  </div>
</template>

<script>
  export default {
    name: 'HelloWorld',
    data() {
      return {
        msg: 'WHO YOU ARE?'
      }
    },
    created() {

    },
    destroyed() {
      this.websock.close()
    },
    methods: {
      initWebSocket() {
        this.websock = new WebSocket("ws://127.0.0.1:9321/ws");
        this.websock.onmessage = this.wsOnmessage;
        this.websock.onopen = this.wsOnopen;
        this.websock.onerror = this.wsOnerror;
        this.websock.onclose = this.wsClose;
      },
      wsOnopen() {
        this.wsSend(JSON.stringify("CC"));
      },
      wsOnerror() {
        this.initWebSocket();
      },
      wsOnmessage(e) {
        this.msg = e.data;
      },
      wsSend(Data) {
        this.websock.send(Data);
      },
      wsClose(e) {
        console.log('断开连接', e);
      },
    },
  }
</script>

<style scoped>
  h1, h2 {
    font-weight: normal;
  }
</style>
