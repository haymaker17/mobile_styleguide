//
//  UploadQueueItemDescriptor.h
//  ConcurMobile
//
//  Created by charlottef on 11/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface UploadQueueItemDescriptor : NSObject
{
    NSString    *uuid;
    int         itemNumber;
}

@property (nonatomic, strong) NSString  *uuid;
@property (nonatomic, assign) int       itemNumber;

@end
