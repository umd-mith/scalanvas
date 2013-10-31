for dir in output/*/*/*
do
  mkdir -p 'compact/'${dir#output/}
  for file in $dir/*
  do
    echo $file
    jq -c '.' $file > 'compact/'${file#output/}
  done
done

tar zcvf compact.tgz compact/

