package com.revy.example.domain;


import org.springframework.data.jpa.repository.JpaRepository;

public interface AiPromptRepository extends JpaRepository<AiPrompt, Long> {
}