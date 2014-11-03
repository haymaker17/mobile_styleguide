//
//  RailChoiceSegmentData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RailChoiceTrainData.h"

@interface RailChoiceSegmentData : NSObject {
	BOOL isReturn;
	int totalTime;
	NSMutableArray		*trains;
	RailChoiceTrainData	*train;
}

@property BOOL isReturn;
@property int totalTime;
@property (strong, nonatomic) NSMutableArray		*trains;
@property (strong, nonatomic) RailChoiceTrainData	*train;

-(id)init;

@end
