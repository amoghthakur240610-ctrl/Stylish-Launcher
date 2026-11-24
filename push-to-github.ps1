<#
.SYNOPSIS
    Push StylishLauncher to GitHub and trigger APK build via GitHub Actions
#>

param(
    [string]$GitHubUsername = "",
    [string]$RepoName = "StylishLauncher"
)

$projectPath = "C:\Users\amogh\Downloads\StylishLauncher"
Set-Location $projectPath

Write-Host "=== Stylish Launcher → GitHub Push Script ===" -ForegroundColor Cyan
Write-Host "Project: $projectPath`n" -ForegroundColor Gray

# 1. Check/Install Git
function Ensure-GitInstalled {
    if (Get-Command git -ErrorAction SilentlyContinue) {
        Write-Host "✅ Git found: $(git --version)" -ForegroundColor Green
        return $true
    }

    Write-Host "⚠️ Git not found. Installing via winget..." -ForegroundColor Yellow
    try {
        winget install --id Git.Git -e --accept-source-agreements --accept-package-agreements
        if (Get-Command git -ErrorAction SilentlyContinue) {
            Write-Host "✅ Git installed: $(git --version)" -ForegroundColor Green
            return $true
        }
    } catch {
        Write-Host "❌ Winget install failed. Please install Git manually from https://git-scm.com" -ForegroundColor Red
        return $false
    }
    return $false
}

# 2. Get GitHub username
function Get-GitHubUsername {
    param([string]$ProvidedUsername)

    if ($ProvidedUsername) { return $ProvidedUsername }

    $username = Read-Host "Enter your GitHub username"
    while (-not $username) {
        Write-Host "Username required!" -ForegroundColor Red
        $username = Read-Host "Enter your GitHub username"
    }
    return $username
}

# 3. Initialize repo
function Init-Repo {
    if (Test-Path ".git") {
        Write-Host "✅ Git repo already initialized" -ForegroundColor Green
        return $true
    }

    Write-Host "🔧 Initializing git repository..." -ForegroundColor Cyan
    git init
    git add .
    git commit -m "Stylish Launcher - Initial commit"
    git branch -M main
    Write-Host "✅ Repository initialized" -ForegroundColor Green
    return $true
}

# 4. Create GitHub repo manually
function Create-GitHubRepo {
    param($username, $repoName)

    Write-Host "🌐 Please create repo manually:" -ForegroundColor Yellow
    Write-Host "   1. Go to: https://github.com/new" -ForegroundColor Gray
    Write-Host "   2. Repository name: $repoName" -ForegroundColor Gray
    Write-Host "   3. Public, NO README/license/gitignore" -ForegroundColor Gray
    Write-Host "   4. Click 'Create repository'" -ForegroundColor Gray
    Write-Host "" -ForegroundColor Gray
    Read-Host "Press Enter after creating the repo..."
    return $true
}

# 5. Push to GitHub
function Push-ToGitHub {
    param($username, $repoName)

    $remoteUrl = "https://github.com/$username/$repoName.git"

    Write-Host "📤 Adding remote: $remoteUrl" -ForegroundColor Cyan
    git remote remove origin 2>$null
    git remote add origin $remoteUrl

    Write-Host "📤 Pushing to GitHub..." -ForegroundColor Cyan
    git push -u origin main

    return $LASTEXITCODE -eq 0
}

# 6. Open Actions page
function Open-ActionsPage {
    param($username, $repoName)
    $url = "https://github.com/$username/$repoName/actions"
    Write-Host "🌐 Opening Actions page: $url" -ForegroundColor Cyan
    Start-Process $url
}

# ===== MAIN =====
# Step 1: Git
if (-not (Ensure-GitInstalled)) { exit 1 }

# Step 2: Username
$username = Get-GitHubUsername -ProvidedUsername $GitHubUsername

# Step 3: Init
if (-not (Init-Repo)) { exit 1 }

# Step 4: Create repo
if (-not (Create-GitHubRepo -username $username -repoName $RepoName)) { exit 1 }

# Step 5: Push
if (-not (Push-ToGitHub -username $username -repoName $RepoName)) {
    Write-Host "❌ Push failed. Check credentials/remote." -ForegroundColor Red
    exit 1
}

Write-Host "`n✅ Push complete!" -ForegroundColor Green

# Step 6: Open Actions
Open-ActionsPage -username $username -repoName $RepoName

Write-Host "`n📱 Next steps:" -ForegroundColor Cyan
Write-Host "   1. Wait ~3 min for build (green checkmark)" -ForegroundColor Gray
Write-Host "   2. Click run → Artifacts → Download 'app-debug-apk'" -ForegroundColor Gray
Write-Host "   3. Unzip → adb install app-debug.apk" -ForegroundColor Gray
Write-Host "   4. adb shell cmd package set-home-activity com.stylishlauncher/.MainActivity" -ForegroundColor Gray