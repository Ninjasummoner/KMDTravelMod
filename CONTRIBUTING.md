# Contributing

Thanks for wanting to improve KMD Travel.

## Before Opening A Pull Request

- Keep changes focused on one feature or bug fix.
- Test the loader/version folder you changed.
- Do not commit build folders, run folders, caches, logs, or local game saves.
- Keep public-facing text clear and player friendly.

## Code Style

- Prefer small helper methods over very large methods.
- Keep loader-specific logic inside that loader/version folder.
- Avoid unrelated formatting churn.
- Name variables for their purpose, not their type.
- Add comments only when they explain why something exists.

## Testing Checklist

- Project builds with the correct Java version.
- Travel posts place, render, and show names.
- The map opens, pans, zooms, and shows pins.
- Recipes load and shared-post crafting respects config.
- Event profiles save per world/server.
- Fast travel resumes correctly after passive, hostile, and command-finished encounters.

## Contribution License

By submitting a pull request, patch, translation, asset, document, issue reproduction, or other contribution, you agree that Ninjasummoner may use it in KMD Travel under the project license.

You still own your original contribution, but the project needs permission to include, modify, distribute, and maintain it as part of KMD Travel.

## Addons

You do not need to open a pull request to make an addon. Addons are allowed when they stay separate and require KMD Travel as a dependency instead of bundling KMD Travel code or assets.

## Good Issues And Pull Requests

Useful reports include the loader, Minecraft version, KMD Travel version, whether it happened on a server or in singleplayer, and a short list of steps that reproduces the problem.
