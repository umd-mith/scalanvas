for dir in output/production/*/*
do
  mkdir -p 'compact/'${dir#output/}
  for file in $dir/*
  do
    echo 'compact/'${file#output/}
    jq -c '.' $file > 'compact/'${file#output/}
    gzip -k 'compact/'${file#output/}
  done
done

tar zcvf compact.tgz compact/

