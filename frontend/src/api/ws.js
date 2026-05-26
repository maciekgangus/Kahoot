import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let client = null

export function connectWS(onConnected) {
  const c = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    reconnectDelay: 0,
    onConnect: () => {
      console.log('[WS] Connected')
      onConnected(c)  // use local ref, not global — avoids StrictMode double-invoke bug
    },
    onDisconnect: () => console.log('[WS] Disconnected'),
    onStompError: (frame) => console.error('[WS] STOMP error', frame),
  })
  client = c
  c.activate()
  return c
}

export function disconnectWS() {
  client?.deactivate()
  client = null
}

export function getClient() {
  return client
}
