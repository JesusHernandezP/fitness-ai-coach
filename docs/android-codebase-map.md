# Android Codebase Map

## Base package

com.fitness.fitnessaicoach

---

## presentation

UI layer

screens
viewmodels
navigation

presentation/auth

login screen
register screen

presentation/dailylog

daily summary screen

presentation/meal

meal creation screen

presentation/workout

workout creation screen

presentation/coach

AI recommendation screen

presentation/navigation

navigation graph

presentation/components

reusable UI components

---

## domain

business logic models

domain/model

User
DailyLog
Food
Meal
Workout
Goal
AIRecommendation

---

domain/repository

interfaces defining data access

---

domain/usecase

application logic

examples:

LoginUseCase
GetTodayLogUseCase
SearchFoodUseCase

---

## data

implementation layer

data/remote/api

Retrofit interfaces

AuthApi
DailyLogApi
FoodApi
WorkoutApi
AiCoachApi

---

data/remote/dto

DTO classes matching backend responses

---

data/remote/mapper

DTO → domain mapping

---

data/repository

repository implementations

---

data/local

DataStore token storage

---

## di

dependency injection modules

NetworkModule

RepositoryModule

UseCaseModule

---

## core

shared utilities

Result wrapper

Constants

Extensions