//
//  FSSegment.h
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FSSegmentOption.h"

@interface FSSegment : NSObject
{
    NSMutableArray *segmentOptions;
}

@property (nonatomic, strong) NSMutableArray *segmentOptions;

-(id)init;
-(void) appendOptions:(NSMutableArray*)ary;

-(FSSegmentOption*)getCurrentSegmentOption;

-(void)startTag:(NSString*)tag;
-(void)endTag:(NSString*)tag withText:(NSString*)text;

@end
