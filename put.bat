rsync -a --progress dist root@104.131.64.201:/opt/greymemory.AnomalyDetector
rsync -a --progress anomaly_detector.sh root@104.131.64.201:/opt/greymemory.AnomalyDetector
rsync -a --progress AnomalyDetector.config sh root@104.131.64.201:/opt/greymemory.AnomalyDetector
rsync -a --progress socket2zmq.py sh root@104.131.64.201:/opt/greymemory.AnomalyDetector
ssh root@104.131.64.201 'chmod +x /opt/greymemory.AnomalyDetector/anomaly_detector.sh'