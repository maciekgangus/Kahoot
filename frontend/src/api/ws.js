import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let client = null

export function connectWS(onConnected) {
  client = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    reconnectDelay: 3000,
    onConnect: () => {
      console.log('[WS] Connected')
      onConnected(client)
    },
    onDisconnect: () => console.log('[WS] Disconnected'),
    onStompError: (frame) => console.error('[WS] STOMP error', frame),
  })
  client.activate()
  return client
}

export function disconnectWS() {
  client?.deactivate()
  client = null
}

export function getClient() {
  return client
}
