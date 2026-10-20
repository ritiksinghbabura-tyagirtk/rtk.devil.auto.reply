# RTK Devil Auto Reply Architecture

## Principles
- Keep UI separate from messaging integrations.
- Keep automation rules independent of Telegram-specific code.
- Declare integration capabilities explicitly.
- Never claim a message was sent unless the adapter confirms success.
- Never commit passwords, OTPs, API hashes or account sessions.

## Package map
- `core/model`: platform-neutral message models.
- `domain/automation`: automation rules and policy.
- `integration/messaging`: common messaging adapter contract.
- `feature/dashboard`: main Compose UI.
- `feature/rules`: future rule editor.
- `feature/messages`: reply message editor.
- `feature/settings`: account and permission settings.
- `data/local`: future persistent local storage.

## Current implementation status
The initial dashboard is a UI prototype. The switches are in-memory only.
No Telegram connection or message sending is implemented yet.

## Adding a messaging integration
1. Implement `MessagingAdapter`.
2. Report truthful capabilities.
3. Handle authentication and disconnection securely.
4. Return explicit send results.
5. Add tests for success, failure, unsupported actions and duplicate events.
6. Document platform restrictions.
