//
//  UploadQueueHelper.h
//  ConcurMobile
//
//  Created by charlottef on 11/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExMsgRespondDelegate.h"

@class UploadQueue;

@interface UploadQueueHelper : NSObject <ExMsgRespondDelegate>
{
    UploadQueue *__weak queue;
}

@property (nonatomic, weak) UploadQueue *queue;

-(id) initWithQueue:(UploadQueue*)uploadQueue;
-(void) updateAfterUpload;

@end
