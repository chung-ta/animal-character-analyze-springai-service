# Deploying Animal Character Analyzer Backend to Render

This guide explains how to deploy the Spring Boot backend service to Render as a Docker container.

## Prerequisites

- GitHub account with the backend code repository
- Render account (free tier works for testing)
- Claude AI API key (optional for MVP testing)

## Deployment Steps

### 1. Push Code to GitHub

First, ensure your code is pushed to GitHub:

```bash
git add .
git commit -m "Prepare for Render deployment"
git push origin main
```

### 2. Create New Web Service on Render

1. Log in to [Render Dashboard](https://dashboard.render.com/)
2. Click **"New +"** → **"Web Service"**
3. Connect your GitHub repository
4. Select `animal-character-analyze-service` repository

### 3. Configure the Service

Use these settings:

- **Name**: `animal-character-analyzer-api`
- **Environment**: `Docker`
- **Region**: Choose closest to your users
- **Branch**: `main` (or `mvp-1.0`)
- **Root Directory**: Leave blank if backend is in repo root
- **Dockerfile Path**: `./Dockerfile`
- **Docker Build Context Directory**: `.`

### 4. Set Environment Variables

In the Render dashboard, add these environment variables:

| Key | Value | Notes |
|-----|-------|-------|
| `CLAUDE_API_KEY` | `your-api-key` | Required for production AI analysis |
| `SPRING_PROFILES_ACTIVE` | `production` | Activates production config |

### 5. Advanced Settings

- **Health Check Path**: `/api/v1/health`
- **Auto-Deploy**: Yes (for automatic deploys on git push)
- **Plan**: Free (upgrade for production)

### 6. Deploy

Click **"Create Web Service"** and Render will:
1. Clone your repository
2. Build the Docker image
3. Deploy the container
4. Provide you with a URL like `https://animal-character-analyzer-api.onrender.com`

## Verification

Once deployed, verify the service is running:

```bash
# Check health endpoint
curl https://your-service-name.onrender.com/api/v1/health

# Expected response:
# {"status":"UP","service":"animal-character-analyzer"}
```

## Connecting Frontend

Update your frontend to use the Render backend URL:

1. In frontend `.env`:
```env
REACT_APP_API_URL=https://your-service-name.onrender.com
```

2. Or update in Vite config:
```js
proxy: {
  '/api': {
    target: 'https://your-service-name.onrender.com',
    changeOrigin: true,
  }
}
```

## Monitoring

- View logs in Render dashboard under "Logs" tab
- Monitor health checks in "Events" tab
- Check metrics in "Metrics" tab

## Troubleshooting

### Service Won't Start
- Check logs for Java/Spring Boot errors
- Verify all required environment variables are set
- Ensure Dockerfile builds successfully locally

### Health Check Failing
- Verify `/api/v1/health` endpoint returns 200 OK
- Check if service needs more startup time
- Review application logs for errors

### CORS Issues
- CORS is configured to allow all origins by default
- Check browser console for specific CORS errors
- Ensure your frontend is making requests to the correct URL

### Memory Issues
- Free tier has 512MB RAM limit
- Monitor memory usage in Metrics
- Adjust `JAVA_OPTS` if needed: `-Xmx400m -Xms200m`

## Production Considerations

1. **Upgrade Plan**: Free tier has limitations (spins down after inactivity)
2. **Custom Domain**: Add your own domain in Settings
3. **Environment Variables**: Never commit sensitive keys
4. **Monitoring**: Set up alerts for downtime
5. **Scaling**: Consider horizontal scaling for high traffic

## Alternative: Direct GitHub Integration

Instead of using `render.yaml`, you can:
1. Fork this repository
2. Connect to Render via GitHub
3. Render will auto-detect the Dockerfile
4. Configure as above

## Support

- [Render Documentation](https://render.com/docs)
- [Render Community](https://community.render.com/)
- Check application logs for debugging