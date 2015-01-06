//
//  RequestHeaderForm.m
//  ConcurMobile
//
//  Created by laurent mery on 18/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RequestHeaderForm.h"

#import "CTETravelRequest.h"
#import "CTEComment.h"
#import "FFField.h"



@interface RequestHeaderForm ()

@end




@implementation RequestHeaderForm {
	
	CTETravelRequest *request;
}

#pragma mark - init

//public
-(void)initFormWithDatas:(CTETravelRequest*)datas{
	
	request = datas;
	[self addForm:@"headerForm" withFormID:[request.HeaderFormID stringValue] isEditable:[request hasPermittedAction:@"save"]];
}



#pragma mark - form Validation (#FFCellValidationProtocol)

//#protocol FFFi
-(NSArray *)errorsOnValidateField:(FFField *)field{
    
    NSMutableArray *errors = [NSMutableArray arrayWithArray:[super errorsOnValidateField:field]];
    
    //TODO: manageValidation
    
    return errors;
}


#pragma mark - filters

-(NSString *)accessForField:(FFField *)field{
    
    NSString *access = [super accessForField:field];
    
    NSArray *fieldsToDisplay = [NSArray arrayWithObjects:
                                @"RequestID",
                                @"Name",
                                @"TotalPostedAmount",
                                @"ApprovalStatusName",
                                @"Currency",
                                @"StartDate",
                                @"EndDate",
                                @"Purpose",
                                @"Comment",
                                nil];
    
    if(![fieldsToDisplay containsObject:[field name]]){
        
        access = @"HD";
    }
    
    return access;
}

-(CTEDataTypes*)dataTypeForFfFieldLight:(FFFieldLight*)ffFieldLight{
    
    CTEDataTypes *dataType = [request valueForKey:[ffFieldLight name]];
    
    if ([@"Comment" isEqualToString:[ffFieldLight name]]) {
        
        CTEComment *comment = [request getLastComment];
        dataType = comment.CommentLight;
    }
    
    return dataType;
}

-(NSString*)iconLabelForField:(FFField*)field{
    
    NSString *iconLabel = [super iconLabelForField:field];
    return iconLabel;
}

@end
