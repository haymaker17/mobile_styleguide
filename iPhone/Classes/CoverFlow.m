//
//  CoverFlow.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CoverFlow.h"


@implementation CoverFlow

// React To User Events

- (void) reactTo:(id)event flow:(int)flow

{
	
	//CGRect rect = GSEventGetLocationInWindow(event);
	
	[cfLayer dragFlow: flow atPoint: rect.origin];
	
}

- (void) mouseUp: (id)event {[self reactTo:event flow:2];}

- (void) mouseDown: (id)event {[self reactTo:event flow:0];}

- (void) mouseDragged: (id)event {[self reactTo:event flow:1];}

- (BOOL) ignoresMouseEvents {return NO;}


// Initialize so it knows about its cfLayer

- (void) setLayer: (id)aLayer {cfLayer = aLayer;}


// Forward the heartbeat

- (void) tick {[cfLayer displayTick];}

@end
