# NNRouteTouristAssistant  
**AI-помощник туриста для Нижнего Новгорода**

NNRouteTouristAssistant - нативное Android приложение, которое помогает туристу составить персональный маршрут прогулки по городу.  
Приложение использует **Yandex MapKit** [Условия использования](https://yandex.ru/legal/maps_api/) для отображения карты и построения маршрутов, а также собственный серверный API для рекомендации точек интереса на основе предпочтений пользователя.

---
<div style="display: flex; flex-wrap: wrap; justify-content: center; gap: 10px;">
  <img width="32%" alt="Screenshot_20251018_171824" src="https://github.com/user-attachments/assets/ae27c7d1-2896-4f89-b4ea-af70d610f895" />
  <img width="32%" alt="502785821-79130c91-38f7-4f85-addd-98b3bb58e2ed" src="https://github.com/user-attachments/assets/e0f08cd7-862e-40a0-8b8e-bf85277190e5" />
  <img width="32%" alt="Screenshot_20251018_153138" src="https://github.com/user-attachments/assets/ed6ba7b5-9759-44fa-88c3-6172eee13e3f" />
</div>

---

***Приложение использует геолокацию для отображения текущего местоположения и построения маршрута. Мы не храним эти данные.***

## Основная функциональность

- выбор подготовленных категорий интересов;
- автоматическая генерация пешеходного маршрута с учётом времени прогулки;
- отображение маршрута и интерактивных точек на карте;
- просмотр таймлайна маршрута с описанием;
- работа в светлой и тёмной теме интерфейса.

## Стек технологий

- **MVVM Паттерн**
- **Kotlin**, **Jetpack Compose**, **Material 3**
- **Hilt (DI)**, **ViewModel**, **StateFlow**
- **Yandex MapKit / Transport API**
- **Coroutines**

## Сборка
### Минимальный SDK: **23 (Android 6.0)**
1. Получите API-ключ для [Yandex MapKit](https://developer.tech.yandex.ru/services/).
2. В корне проекта создайте файл `local.properties`, добавьте строку:

```properties
MAPKIT_API_KEY=ваш_ключ_сюда
```
---
