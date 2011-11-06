set FROMSVN=https://svn.kenai.com/svn/osmv~subversion 
set TOSVN=https://osmv.googlecode.com/svn

svnsync init %FROMSVN% %TOSVN%

svnsync --non-interactive sync %TOSVN%