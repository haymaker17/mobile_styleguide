//
//  FSSegmentOption.h
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FSFlight.h"

@interface FSSegmentOption : NSObject
{
    NSString *sId;
    NSString *travelConfigId;
    NSMutableArray *flights;
    int totalElapsedTime;
}

@property (nonatomic, strong) NSString *sId;
@property (nonatomic, strong) NSString *travelConfigId;
@property (nonatomic, strong) NSMutableArray *flights;
@property int totalElapsedTime;



-(FSFlight*) getCurrentFlight;
-(NSString*) carrierText;

-(void)startTag:(NSString*)tag;
-(void)endTag:(NSString*)tag withText:(NSString*)text;

@end
