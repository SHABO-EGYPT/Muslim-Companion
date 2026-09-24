# Protocol: "Update all" Workflow

When the user says **"Update all"** (or case-insensitive variations like "update all", "Update All"):

You must automatically perform the complete synchronization workflow:

1. **Update `README.md`:**
   - Ensure the latest release version, release notes, badges, and features reflect the current state.
   - Keep both English and Arabic sections accurate.

2. **Update `PROJECT_MEMORY.md`:**
   - Update `Current Release Version` and `Date`.
   - Document any new features, architectural refactoring, bug fixes, or milestones in the changelog section.
   - Maintain the roadmap and outstanding tasks.

3. **Update `PROJECT_MINDMAP.md`:**
   - Synchronize any newly added ViewModels, Services, Receivers, Repositories, or screen flows.
   - Keep Mermaid diagrams and component topology aligned with the latest architecture.

4. **Update GitHub:**
   - Stage all updated files (`git add -A`).
   - Commit with a clear, descriptive message (e.g., `docs: update README, memory, and mindmap specifications`).
   - Push all commits and tags to origin (`git push origin main --tags`).
