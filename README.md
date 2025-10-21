# NNRouteTouristAssistant  
**AI-помощник туриста для Нижнего Новгорода**

NNRouteTouristAssistant - нативное Android приложение, которое помогает туристу составить и пройти персональный маршрут прогулки по городу.  
Приложение использует **Yandex MapKit** [Условия использования](https://yandex.ru/legal/maps_api/) для отображения карты и построения маршрутов, а также собственный серверный API для рекомендации точек интереса на основе предпочтений пользователя.

---
<div style="display: flex; flex-wrap: wrap; justify-content: center; gap: 10px;">
  <img width="32%" alt="Screenshot_20251021_012014" src="https://github.com/user-attachments/assets/4c0519ec-fc81-4fde-9bc0-3775fef16cb0" />
  <img width="32%" alt="Screenshot_20251021_012243" src="https://github.com/user-attachments/assets/7cd9cad5-4a53-491f-848d-8be1520e9839" />
  <img width="32%" alt="Screenshot_20251021_011728" src="https://github.com/user-attachments/assets/67ce59a9-d466-49b4-9ad8-b5fdc1157ad0" />
</div>

---

***Приложение использует геолокацию для отображения текущего местоположения и построения маршрута. Мы не храним эти данные.***

## Основная функциональность

- выбор среди подготовленных категорий интересов;
- генерация персонального пешеходного маршрута с учётом геолокации, интересов пользователя и выбранного времени прогулки;
- навигация по городу в реальном времени, интерактивная карта;
- просмотр таймлайна маршрута с описанием мест;
- работа в светлой и тёмной теме интерфейса.

## Стек

- Паттерн MVVM
- Kotlin, Coroutines, Jetpack Compose, Material 3
- Hilt (DI), **ViewModel, StateFlow
- Yandex MapKit API

## Сборка
### Минимальный SDK: **23 (Android 6.0)**
1. Получите API-ключ для [Yandex MapKit](https://developer.tech.yandex.ru/services/).
2. В корне проекта создайте файл `local.properties`, добавьте строку:

```properties
MAPKIT_API_KEY=key
```
---
