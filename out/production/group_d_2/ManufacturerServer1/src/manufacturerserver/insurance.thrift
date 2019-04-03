namespace java manufacturerserver

service DataRequest
{
     map<string, string> getServerData(1:string fileName)
}
