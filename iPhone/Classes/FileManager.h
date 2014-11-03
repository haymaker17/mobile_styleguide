//
//  FileManager.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface FileManager : NSObject 
{

}

+(void)cleanCache;
+(void)removeReceiptCacheFiles;
+(void) backUpCachedReceiptsFileFrom:(NSString*)srcPath toFile:(NSString*)backUpPath;
+(void)cleanOldLogs;

@end
