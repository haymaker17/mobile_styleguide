//
//  RequestSegmentForm.m
//  ConcurMobile
//
//  Created by Laurent Mery on 04/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RequestSegmentForm.h"
#import "CTETravelRequestSegment.h"

@implementation RequestSegmentForm {
    
    NSArray *segments;
	CTETravelRequestSegment *segment;
}

#pragma mark - form & fields

//public
-(void)initFormWithDatas:(NSArray*)datas{
	
	NSInteger index = 0;
	segments = datas;
	for (segment in segments){
		
        [self addForm:[NSString stringWithFormat:@"%ld",(long)index++] withFormID:[segment.SegmentFormID stringValue] isEditable:NO/*[segment hasPermittedAction:@"save"]*/];
	}
}




#pragma mark - Field at IndexPath



@end
