import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AIChatMessageDto, ApiService } from '../../core/api/api.service';

type ChatMessage = {
  role: 'USER' | 'ASSISTANT';
  message: string;
  createdAt?: string;
};

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit {
  private readonly apiService = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);

  protected messages: ChatMessage[] = [];
  protected draftMessage = '';
  protected isLoadingHistory = true;
  protected isSending = false;
  protected errorMessage = '';

  ngOnInit(): void {
    this.loadHistory(true);
  }

  protected sendMessage(): void {
    const message = this.draftMessage.trim();
    if (!message || this.isSending) {
      return;
    }

    this.errorMessage = '';
    this.isSending = true;

    const optimisticUserMessage: ChatMessage = {
      role: 'USER',
      message,
      createdAt: new Date().toISOString()
    };

    this.messages = [...this.messages, optimisticUserMessage];
    this.draftMessage = '';

    const subscription = this.apiService.sendChatMessage({ message }).subscribe({
      next: () => {
        this.isSending = false;
        this.loadHistory(false);
      },
      error: () => {
        this.messages = this.messages.filter((chatMessage) => chatMessage !== optimisticUserMessage);
        this.errorMessage = 'Unable to send your message right now.';
        this.isSending = false;
      }
    });

    this.destroyRef.onDestroy(() => subscription.unsubscribe());
  }

  protected trackByTimestamp(index: number, message: ChatMessage): string {
    return `${message.role}-${message.createdAt ?? index}-${message.message}`;
  }

  private toChatMessage(message: AIChatMessageDto): ChatMessage {
    return {
      role: message.role,
      message: message.message,
      createdAt: message.createdAt
    };
  }

  private loadHistory(showLoading: boolean): void {
    if (showLoading) {
      this.isLoadingHistory = true;
    }

    const subscription = this.apiService.getChatHistory().subscribe({
      next: (messages) => {
        this.errorMessage = '';
        this.messages = messages.map((message) => this.toChatMessage(message));
        this.isLoadingHistory = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load chat history right now.';
        this.isLoadingHistory = false;
      }
    });

    this.destroyRef.onDestroy(() => subscription.unsubscribe());
  }
}
