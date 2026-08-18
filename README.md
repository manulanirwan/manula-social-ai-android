# Manula Social AI Android

An AI-powered multi-platform social media content generator for Android, powered by Google's Gemini API.

Manula Social AI turns one content idea into platform-specific publishing packages for YouTube, TikTok, Instagram, Threads, LinkedIn, Facebook, and X.

> **Note:** Manula Social AI is an independent project and is not affiliated with or endorsed by Google or any of the supported social media platforms.

## Features

* Generate content from a single idea
* Sinhala support
* English support
* Sinhala-English mixed input
* Automatic language understanding
* Multi-platform content generation
* YouTube content generation
* TikTok content generation
* Instagram content generation
* Threads content generation
* LinkedIn content generation
* Facebook content generation
* X content generation
* Platform-specific titles and captions
* YouTube descriptions
* Hooks
* Calls to action
* Hashtag generation
* Keyword suggestions
* YouTube tags where applicable
* Optional pinned comments
* Optional YouTube chapters
* Platform-specific formatting
* Platform-specific content validation
* Character counters
* Automatic length correction
* Content variations
* Regenerate individual sections
* Content editing
* Copy individual fields
* Copy complete platform packages
* Saved projects
* Project history
* Favorites
* Brand profiles
* Custom audience settings
* Tone selection
* Content goal selection
* Light and dark themes

## Supported Platforms

| Platform  | Content Support                           |
| --------- | ----------------------------------------- |
| YouTube   | Shorts and long-form metadata             |
| TikTok    | Captions, hooks, hashtags, CTAs           |
| Instagram | Reels, posts, carousel captions, hashtags |
| Threads   | Posts and optional threads                |
| LinkedIn  | Professional posts and hashtags           |
| Facebook  | Posts, CTAs, hashtags                     |
| X         | Posts, alternatives, optional threads     |

The app keeps platform-specific generation separate so that one generic caption is not simply copied across every network.

## How It Works

```text
Your Idea
   |
   v
Manula Social AI
   |
   +----> YouTube
   |
   +----> TikTok
   |
   +----> Instagram
   |
   +----> Threads
   |
   +----> LinkedIn
   |
   +----> Facebook
   |
   +----> X
```

The Gemini-powered generation process considers:

* Content topic
* Content type
* Language
* Audience
* Tone
* Goal
* Platform
* Character limits
* Formatting requirements
* Hashtag relevance

## Example

Input:

```text
I built a website using AI and want to create a short video about it.
```

The application can generate separate packages for:

### YouTube

* Title
* Description
* Hashtags
* Tags
* CTA
* Pinned comment

### TikTok

* Caption
* Hook
* Hashtags
* CTA
* On-screen text suggestion

### Instagram

* Caption
* Hook
* CTA
* Hashtags
* Cover text

### Threads

* Post
* Optional thread
* Discussion prompt

### LinkedIn

* Professional hook
* Main post
* CTA
* Hashtags
* First-comment suggestion

### Facebook

* Main post
* Short version
* CTA
* Discussion question

### X

* Standard post
* Alternative version
* Optional thread
* CTA

## AI Content Validation

Before displaying generated content, the application can check:

* Character limits
* Platform fit
* Grammar
* Spelling
* Repetition
* Hashtag relevance
* CTA quality
* Keyword placement
* Unnatural wording
* Unsupported claims

The app distinguishes between hard platform limits and editorial recommendations.

## Tech Stack

* Kotlin
* Android
* Jetpack libraries
* Jetpack Compose where compatible
* Gemini API
* Local storage/database
* Android 8.1-compatible APIs

## Target Device

The primary target is older Android hardware, including:

* Android 8.1 Oreo
* API 27
* Vivo Y93

The application is designed to remain lightweight and responsive on lower-end devices.

## Architecture

```text
Android App
     |
     v
ViewModel
     |
     v
Repository
     |
     v
Content Generation Service
     |
     +----> Platform Rules
     |
     +----> Validation
     |
     v
Gemini API
```

Platform-specific rules are kept separate from the UI so they can be updated without redesigning the application.

## Security

Do not hard-code a production Gemini API key into the APK.

For production deployments, use:

```text
Android App
     |
     v
Secure Backend
     |
     v
Gemini API
```

Never commit API keys, credentials, or other secrets to GitHub.

## Project Status

This project is under active development.

The current focus is creating a fast, practical social media content assistant for creators who want to turn one idea into multiple platform-ready content packages.

## Roadmap

* Improve platform-specific generation
* Improve current platform rules
* Add more content templates
* Improve brand profile support
* Add content scheduling integrations
* Add export options
* Improve generation quality
* Add secure backend infrastructure
* Improve offline project management
* Improve performance on older Android devices

## Disclaimer

Manula Social AI is an independent application.

YouTube, TikTok, Instagram, Threads, LinkedIn, Facebook, X, Google, and Gemini are trademarks of their respective owners.

This project does not guarantee reach, engagement, virality, ranking, or platform performance.

## License

Add your preferred license before publishing the project for external contributions.
