def main(data):
    wordlist=["help","bacho","leave","bachao","live","leaf"]
    userlist=data.split()
    for i in wordlist:
        if userlist.count(i)>=3:
            break
    else:
        return 0
    return 1