import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, OnInit, ViewChild, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AIChatMessageDto, ApiService } from '../../core/api/api.service';
import { SpinnerComponent } from '../../shared/components/spinner/spinner.component';

type ChatMessage = {
  role: 'USER' | 'ASSISTANT';
  message: string;
  createdAt?: string;
};

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, SpinnerComponent],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css',
})
export class ChatComponent implements OnInit {
  private readonly apiService = inject(ApiService);
  @ViewChild('messagesContainer') private messagesContainer?: ElementRef<HTMLDivElement>;

  protected messages: ChatMessage[] = [];
  protected draftMessage = '';
  protected isLoadingHistory = true;
  protected isSending = false;
  protected errorMessage = '';

  ngOnInit(): void {
    this.loadHistory(true);
  }

  ngAfterViewInit(): void {
    this.scrollToBottom();
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
      createdAt: new Date().toISOString(),
    };

    this.messages = [...this.messages, optimisticUserMessage];
    this.draftMessage = '';
    this.scrollToBottom();

    this.apiService.sendChatMessage({ message }).subscribe({
      next: () => {
        this.isSending = false;
        this.loadHistory(false);
      },
      error: () => {
        this.messages = this.messages.filter(
          (chatMessage) => chatMessage !== optimisticUserMessage,
        );
        this.errorMessage = 'No se pudo enviar tu mensaje en este momento.';
        this.isSending = false;
      },
    });
  }

  protected trackByTimestamp(index: number, message: ChatMessage): string {
    return `${message.role}-${message.createdAt ?? index}-${message.message}`;
  }

  protected formatMessageTimestamp(createdAt?: string): string {
    const date = createdAt ? new Date(createdAt) : new Date();
    if (Number.isNaN(date.getTime())) {
      return '';
    }

    const time = new Intl.DateTimeFormat('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);

    const now = new Date();
    const isSameDay =
      now.getDate() === date.getDate() &&
      now.getMonth() === date.getMonth() &&
      now.getFullYear() === date.getFullYear();

    if (isSameDay) {
      return time;
    }

    const day = new Intl.DateTimeFormat('es-ES', {
      day: '2-digit',
      month: '2-digit',
    }).format(date);

    return `${time} · ${day}`;
  }

  private toChatMessage(message: AIChatMessageDto): ChatMessage {
    return {
      role: message.role,
      message: message.message,
      createdAt: message.createdAt ?? new Date().toISOString(),
    };
  }

  private loadHistory(showLoading: boolean): void {
    if (showLoading) {
      this.isLoadingHistory = true;
    }

    this.apiService.getChatHistory().subscribe({
      next: (messages) => {
        this.errorMessage = '';
        this.messages = messages.map((message) => this.toChatMessage(message));
        this.isLoadingHistory = false;
        this.scrollToBottom();
      },
      error: () => {
        this.errorMessage = 'No se pudo cargar el historial del chat en este momento.';
        this.isLoadingHistory = false;
      },
    });
  }

  private scrollToBottom(): void {
    window.requestAnimationFrame(() => {
      const container = this.messagesContainer?.nativeElement;
      if (!container) {
        return;
      }

      container.scrollTop = container.scrollHeight;
    });
  }
}
