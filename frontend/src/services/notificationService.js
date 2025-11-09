import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let client = null;

export const connectNotifications = (onMessage) => {
  if (client && client.connected) return client;

  const wsUrl = 'http://localhost:9090/ws';
  client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
      console.log('WebSocket connected');
      client.subscribe('/topic/alerts', (message) => {
        try {
          const data = JSON.parse(message.body);
          onMessage(data);
        } catch (e) {
          console.error('Failed to parse notification', e);
        }
      });
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame);
    },
    onWebSocketError: (error) => {
      console.error('WebSocket error:', error);
    },
    onDisconnect: () => {
      console.log('WebSocket disconnected');
    }
  });

  client.activate();
  return client;
};

export const disconnectNotifications = () => {
  if (client && client.connected) {
    client.deactivate();
    client = null;
  }
};

export default { connectNotifications, disconnectNotifications };

