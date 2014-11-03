//
//  ExMsgRespondDelegate.h
//  ConcurMobile
//
//  Created by yiwen on 3/11/11.
//  Copyright 2011 Concur. All rights reserved.
//

// Protocol to allow any object to receive callback upon receiving and parsing of a msg
//  In addition to the API defined below, implementor of this protocol should call
//    [MsgHandler cancelAllRequestsForDelegate:self]; in dealloc
#import <Foundation/Foundation.h>

@class Msg;

@protocol ExMsgRespondDelegate

//
// The respondToFoundData method is still used by MobileViewController and MobileTableViewController
// and their subclasses.  Other implementors of ExMsgRespondDelegate should implement didProcessMessage.
// Please see MobileViewController.didProcessMessage for an example of how to implement it.
//
// Instead of didProcessMessage, we used to call respondToFoundData.  However, most of the existing
// respondToFoundData methods did not check whether we actually had data, i.e. whether we actually
// had data from either the server of the cache.  They just assumed the data was present.  Therefore,
// didProcessMessage will check for such data and call respondToFoundData when it is present.
// Otherwise, it should handle the lack of data appropriately.  See MobileViewController's
// didProcessMessage method for an example.
//
-(void) didProcessMessage:(Msg *)msg;

@end
