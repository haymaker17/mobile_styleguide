//
//  GovAttachExpToDocData.h
//  ConcurMobile
//
//  Created by Shifan Wu on 1/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MsgResponderCommon.h"

@interface GovAttachExpToDocData : MsgResponderCommon
{
    NSArray         *expId;
    NSString        *docType;
    NSString        *docName;
    
    NSMutableDictionary     *expUploadStatus;
    BOOL                    overAllStatus;
    
    BOOL                    inSignleExp;
}
@property (nonatomic, strong) NSArray           *expId;
@property (nonatomic, strong) NSString          *docType;
@property (nonatomic, strong) NSString          *docName;
@property (nonatomic, strong) NSMutableDictionary       *expUploadStatus;
@property BOOL          overAllStatus;
@property BOOL          inSignleExp;

-(Msg*) newMsg:(NSMutableDictionary*)parameterBag;
-(NSString *) makeXMLBody;

@end
