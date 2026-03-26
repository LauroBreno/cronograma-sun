# Cronograma Acadêmico - Sun

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)

Este projeto é um aplicativo **Android Nativo** desenvolvido como solução definitiva para a gestão de cronogramas e desempenho acadêmico.

Ele surgiu de uma necessidade real de substituir uma ferramenta de visualização de estudos feita em Power BI, que apresentava sérias limitações de performance e usabilidade em dispositivos mobile de entrada.

---

## Interface do Usuário (UI)

<p align="center">
    ![Image](https://github.com/user-attachments/assets/8c94a7d4-734e-479a-9212-58e7acb9eee2)
    ![Image](https://github.com/user-attachments/assets/5bdf0e96-b5b4-46cc-aacf-46c24bbd09d2)
    ![Image](https://github.com/user-attachments/assets/95c7be6f-eb03-4128-964e-c94f2a89754d)
</p>

---

## Evolução do Projeto e Objetivos

O objetivo principal da arquitetura deste aplicativo foi migrar uma estrutura de dados analítica de um dashboard pesado para um ambiente **Mobile Nativo**, garantindo:

* **Alta Performance:** Execução fluida, animações nativas e transições rápidas em dispositivos com hardware limitado (Homologado e testado em um Xiaomi Mi 9 SE).
* **Disponibilidade Offline:** Armazenamento local (Local-first) para consulta instantânea de matérias, notas e assuntos, sem depender de requisições web.
* **Conforto Visual (UX/UI):** Interface customizada utilizando cores pastéis (Material Design 3) e microinterações pensadas para reduzir a ansiedade e proporcionar conforto durante longos períodos de uso contínuo.

---

## Tecnologias e Arquitetura

O ecossistema do projeto foi construído priorizando estabilidade e o uso de bibliotecas nativas e modernas do desenvolvimento Android:

* **Linguagem:** Java (Android SDK)
* **Persistência de Dados:** SQLite (com `DatabaseHelper` nativo)
* **UI / UX:** Material Design 3, `ViewPager2` (para carrossel magnético de cards) e `ConstraintLayout`.
* **Animações:** [Lottie by Airbnb](https://lottiefiles.com/) (Microinterações otimizadas via `.json`).
* **Controle de Versão:** Git & GitHub
* **Ambiente de Desenvolvimento:** Construído em Linux Mint (rodando em um MacBook Pro 2012), provando que código limpo e otimizado nasce da eficiência do desenvolvedor, não apenas do hardware.

---

##  Autor

**Lauro Breno**
*Engenheiro Químico & Desenvolvedor de Sistemas (ADS)*

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/lauro-freitas-b2850510a/)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/LauroBreno?tab=repositories)

---
*Projeto desenvolvido com foco em performance mobile e experiência do usuário (UX).*