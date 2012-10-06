# IrssiBot start script for Windows platforms

echo "starting IrssiBot.. "
java -classpath "jars/xerces.jar:jars/mysql.jar:." irssibot.core.Core > bot.log 
echo "[done]"
